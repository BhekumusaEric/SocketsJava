"use client";

import { useState, useEffect, useRef } from "react";
import { pusherClient } from "@/lib/pusher";
import { Send, User, MessageCircle, Sparkles } from "lucide-react";
import { motion, AnimatePresence } from "framer-motion";
import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";

function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

interface Message {
  user: string;
  message: string;
  color: string;
  timestamp: string;
}

export default function SocialConnect() {
  const [username, setUsername] = useState("");
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [messages, setMessages] = useState<Message[]>([]);
  const [newMessage, setNewMessage] = useState("");
  const [userColor] = useState(() => 
    `hsl(${Math.floor(Math.random() * 360)}, 70%, 65%)`
  );
  
  const scrollRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!isLoggedIn) return;

    const channel = pusherClient.subscribe("chat-room");
    channel.bind("new-message", (data: Message) => {
      setMessages((prev) => [...prev, data].slice(-50)); // Keep last 50
    });

    return () => {
      pusherClient.unsubscribe("chat-room");
    };
  }, [isLoggedIn]);

  useEffect(() => {
    scrollRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  const handleLogin = (e: React.FormEvent) => {
    e.preventDefault();
    if (username.trim()) setIsLoggedIn(true);
  };

  const sendMessage = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newMessage.trim()) return;

    const msgContent = newMessage;
    setNewMessage("");

    await fetch("/api/messages", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        message: msgContent,
        user: username,
        color: userColor,
      }),
    });
  };

  if (!isLoggedIn) {
    return (
      <div className="min-h-screen bg-[#0a0a0b] flex items-center justify-center p-4">
        <motion.div 
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="w-full max-w-md bg-white/5 border border-white/10 p-8 rounded-3xl backdrop-blur-xl shadow-2xl"
        >
          <div className="flex justify-center mb-6">
            <div className="bg-gradient-to-tr from-cyan-500 to-purple-500 p-4 rounded-2xl shadow-lg ring-1 ring-white/20">
              <MessageCircle className="w-8 h-8 text-white" />
            </div>
          </div>
          <h1 className="text-3xl font-bold text-center bg-gradient-to-r from-white to-white/60 bg-clip-text text-transparent mb-2">
            Social Connect
          </h1>
          <p className="text-white/40 text-center mb-8 text-sm">
            The next generation of student networking.
          </p>
          <form onSubmit={handleLogin} className="space-y-4">
            <div className="space-y-2">
              <label className="text-xs font-semibold text-white/50 ml-1">USERNAME</label>
              <input
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="How should we call you?"
                className="w-full bg-white/5 border border-white/10 rounded-2xl px-5 py-4 text-white placeholder:text-white/20 focus:outline-none focus:ring-2 focus:ring-purple-500/50 transition-all font-medium"
                required
              />
            </div>
            <button
              type="submit"
              className="w-full bg-gradient-to-r from-cyan-600 to-purple-600 hover:from-cyan-500 hover:to-purple-500 text-white font-bold py-4 rounded-2xl shadow-xl shadow-purple-500/20 transform transition-all active:scale-95 flex items-center justify-center gap-2"
            >
              <Sparkles className="w-5 h-5" />
              Join the Chat
            </button>
          </form>
        </motion.div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[#0a0a0b] flex flex-col p-4 md:p-8">
      {/* Header */}
      <div className="max-w-4xl w-full mx-auto flex items-center justify-between mb-8">
        <div className="flex items-center gap-3">
          <div className="bg-white/5 p-2 rounded-xl border border-white/10">
            <MessageCircle className="w-6 h-6 text-cyan-400" />
          </div>
          <h2 className="text-xl font-bold text-white tracking-tight">Social Connect</h2>
        </div>
        <div className="flex items-center gap-2 px-3 py-1.5 bg-white/5 rounded-full border border-white/10">
          <div className="w-2 h-2 rounded-full bg-green-500 animate-pulse" />
          <span className="text-xs font-medium text-white/70 uppercase tracking-wider">{username}</span>
        </div>
      </div>

      {/* Chat Box */}
      <div className="max-w-4xl w-full mx-auto flex-1 flex flex-col bg-white/5 border border-white/10 rounded-[2.5rem] overflow-hidden backdrop-blur-md shadow-2xl">
        <div className="flex-1 overflow-y-auto p-6 space-y-6 scrollbar-hide">
          <AnimatePresence initial={false}>
            {messages.map((msg, i) => (
              <motion.div
                key={msg.timestamp + i}
                initial={{ opacity: 0, x: -10, scale: 0.95 }}
                animate={{ opacity: 1, x: 0, scale: 1 }}
                className={cn(
                  "flex items-start gap-4",
                  msg.user === username ? "flex-row-reverse" : "flex-row"
                )}
              >
                <div 
                  className="w-10 h-10 rounded-2xl flex-shrink-0 flex items-center justify-center border border-white/10"
                  style={{ backgroundColor: `${msg.color}20`, color: msg.color }}
                >
                  <User className="w-6 h-6" />
                </div>
                <div className={cn(
                  "flex flex-col gap-1.5",
                  msg.user === username ? "items-end" : "items-start"
                )}>
                  <span className="text-[10px] font-bold text-white/30 px-1 uppercase tracking-widest">
                    {msg.user} • {new Date(msg.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                  </span>
                  <div className={cn(
                    "px-5 py-3 rounded-[1.5rem] text-sm font-medium",
                    msg.user === username 
                      ? "bg-purple-600 text-white rounded-tr-none shadow-lg shadow-purple-600/10" 
                      : "bg-white/5 text-white/90 border border-white/10 rounded-tl-none"
                  )}>
                    {msg.message}
                  </div>
                </div>
              </motion.div>
            ))}
          </AnimatePresence>
          <div ref={scrollRef} />
        </div>

        {/* Input */}
        <div className="p-6 bg-white/5 border-t border-white/10">
          <form onSubmit={sendMessage} className="flex gap-4">
            <input
              type="text"
              value={newMessage}
              onChange={(e) => setNewMessage(e.target.value)}
              placeholder="Message the network..."
              className="flex-1 bg-white/5 border border-white/10 rounded-2xl px-6 py-4 text-white placeholder:text-white/20 focus:outline-none focus:ring-2 focus:ring-cyan-500/50 transition-all"
            />
            <button
              type="submit"
              className="bg-cyan-600 hover:bg-cyan-500 text-white p-4 rounded-2xl shadow-xl shadow-cyan-600/20 transform transition-all active:scale-95"
            >
              <Send className="w-6 h-6" />
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
