import { NextResponse } from "next/server";
import { pusherServer } from "@/lib/pusher";

export async function POST(req: Request) {
  const { message, user, color } = await req.json();

  await pusherServer.trigger("chat-room", "new-message", {
    message,
    user,
    color,
    timestamp: new Date().toISOString(),
  });

  return NextResponse.json({ success: true });
}
