// create-razorpay-order
//
// Runs on the Supabase Edge Functions runtime (Deno). Invoked by the Android client
// immediately before Razorpay checkout opens.
//
// Why this exists: the client used to send the amount it wanted to pay. Here the
// amount is recomputed from the buyer's cart rows on the server, so a modified APK
// cannot name its own price. The Razorpay order is created against that amount and
// the resulting order row is written as `pending` with no UPDATE policy for clients.

import { createClient } from "npm:@supabase/supabase-js@2";

const RAZORPAY_API = "https://api.razorpay.com/v1/orders";

interface CartRow {
  product_id: string;
  quantity: number;
  products: {
    price: string;
    title: string;
  } | null;
}

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers":
    "authorization, x-client-info, apikey, content-type",
};

function json(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { ...corsHeaders, "Content-Type": "application/json" },
  });
}

/** Prices arrive as display strings ("34,999") — same rule the app applies. */
function parsePrice(raw: string): number {
  const cleaned = raw.replace(/[^0-9.]/g, "");
  const value = Number.parseFloat(cleaned);
  return Number.isFinite(value) ? value : 0;
}

Deno.serve(async (request) => {
  if (request.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  try {
    const supabaseUrl = Deno.env.get("SUPABASE_URL")!;
    const anonKey = Deno.env.get("SUPABASE_ANON_KEY")!;
    const serviceRoleKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;
    const razorpayKeyId = Deno.env.get("RAZORPAY_KEY_ID")!;
    const razorpayKeySecret = Deno.env.get("RAZORPAY_KEY_SECRET")!;

    // Identity comes from the caller's JWT, never from the request body.
    const userClient = createClient(supabaseUrl, anonKey, {
      global: { headers: { Authorization: request.headers.get("Authorization") ?? "" } },
    });
    const {
      data: { user },
      error: userError,
    } = await userClient.auth.getUser();
    if (userError || !user) {
      return json({ error: "unauthorized" }, 401);
    }

    const service = createClient(supabaseUrl, serviceRoleKey);

    const { data: cart, error: cartError } = await service
      .from("cart_items")
      .select("product_id, quantity, products(price, title)")
      .eq("user_id", user.id);
    if (cartError) return json({ error: cartError.message }, 500);

    const rows = (cart ?? []) as unknown as CartRow[];
    if (rows.length === 0) return json({ error: "cart is empty" }, 400);

    let amount = 0;
    let itemCount = 0;
    const items: Record<string, number> = {};
    for (const row of rows) {
      if (!row.products) continue;
      const quantity = Number(row.quantity);
      amount += parsePrice(row.products.price) * quantity;
      itemCount += quantity;
      items[row.product_id] = quantity;
    }
    if (itemCount === 0) return json({ error: "cart is empty" }, 400);

    // Razorpay expects integer paise.
    const amountPaise = Math.round(amount * 100);

    const razorpayResponse = await fetch(RAZORPAY_API, {
      method: "POST",
      headers: {
        Authorization:
          "Basic " + btoa(`${razorpayKeyId}:${razorpayKeySecret}`),
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        amount: amountPaise,
        currency: "INR",
        receipt: user.id,
        notes: { supabase_user: user.id },
      }),
    });
    if (!razorpayResponse.ok) {
      const detail = await razorpayResponse.text();
      return json({ error: "razorpay_order_failed", detail }, 502);
    }
    const razorpayOrder = await razorpayResponse.json();

    const { data: order, error: insertError } = await service
      .from("orders")
      .insert({
        user_id: user.id,
        amount,
        item_count: itemCount,
        status: "pending",
        razorpay_order_id: razorpayOrder.id,
        items,
      })
      .select("id")
      .single();
    if (insertError) return json({ error: insertError.message }, 500);

    return json({
      supabaseOrderId: order.id,
      razorpayOrderId: razorpayOrder.id,
      amount: amountPaise,
      currency: "INR",
    });
  } catch (cause) {
    return json({ error: String(cause) }, 500);
  }
});
