// verify-payment
//
// Called from the Android app with the triple Razorpay hands back after checkout:
// razorpay_payment_id, razorpay_order_id, razorpay_signature.
//
// The signature is HMAC-SHA256("order_id|payment_id", RAZORPAY_KEY_SECRET) computed by
// Razorpay. Recomputing it here — with the secret that never leaves the server — is what
// makes the client's claim trustworthy. On success the `mark_order_paid` RPC flips the
// order to paid and clears the cart in one transaction.

import { createClient } from "npm:@supabase/supabase-js@2";

interface VerifyBody {
  paymentId: string;
  razorpayOrderId: string;
  signature: string;
  supabaseOrderId: string;
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

function timingSafeEqual(a: string, b: string): boolean {
  if (a.length !== b.length) return false;
  let diff = 0;
  for (let i = 0; i < a.length; i++) {
    diff |= a.charCodeAt(i) ^ b.charCodeAt(i);
  }
  return diff === 0;
}

Deno.serve(async (request) => {
  if (request.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  try {
    const supabaseUrl = Deno.env.get("SUPABASE_URL")!;
    const anonKey = Deno.env.get("SUPABASE_ANON_KEY")!;
    const serviceRoleKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;
    const keySecret = Deno.env.get("RAZORPAY_KEY_SECRET")!;

    const userClient = createClient(supabaseUrl, anonKey, {
      global: { headers: { Authorization: request.headers.get("Authorization") ?? "" } },
    });
    const {
      data: { user },
    } = await userClient.auth.getUser();
    if (!user) return json({ error: "unauthorized" }, 401);

    const body = (await request.json()) as VerifyBody;
    if (!body?.paymentId || !body?.razorpayOrderId || !body?.signature) {
      return json({ error: "missing payment fields" }, 400);
    }

    // Razorpay's signature = HMAC-SHA256("order_id|payment_id", key_secret).
    const message = `${body.razorpayOrderId}|${body.paymentId}`;
    const hmacKey = await crypto.subtle.importKey(
      "raw",
      new TextEncoder().encode(keySecret),
      { name: "HMAC", hash: "SHA-256" },
      false,
      ["sign"],
    );
    const signed = await crypto.subtle.sign(
      "HMAC",
      hmacKey,
      new TextEncoder().encode(message),
    );
    const expectedSignature = Array.from(new Uint8Array(signed))
      .map((b) => b.toString(16).padStart(2, "0"))
      .join("");

    if (!timingSafeEqual(expectedSignature, body.signature)) {
      return json({ error: "signature mismatch" }, 400);
    }

    const service = createClient(supabaseUrl, serviceRoleKey);

    const { data: order, error: orderError } = await service
      .from("orders")
      .select("id, user_id, status")
      .eq("id", body.supabaseOrderId)
      .single();
    if (orderError || !order) return json({ error: "unknown order" }, 404);
    if (order.user_id !== user.id) return json({ error: "not your order" }, 403);

    const { error: rpcError } = await service.rpc("mark_order_paid", {
      p_order_id: order.id,
      p_payment_id: body.paymentId,
      p_user_id: user.id,
    });
    if (rpcError) {
      // Order was already settled (double callback) — that is idempotent, not an error.
      if (!/not a pending order/.test(rpcError.message)) {
        return json({ error: rpcError.message }, 500);
      }
    }

    return json({ ok: true });
  } catch (cause) {
    return json({ error: String(cause) }, 500);
  }
});
