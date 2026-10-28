"use client";
import { createContext, useContext, useEffect, useState, useCallback, type ReactNode } from "react";
import { demoStorage } from "@/lib/demo-storage";
import { demoNotifications, type NotificationCategory } from "@/lib/notifications";
import { allPages, apiMode, request } from "@/lib/session";
import { useAuth } from "./auth-provider";
export type NotificationItem = {id:string; category:NotificationCategory; title:string; description:string; time:string; unread:boolean; accountId?:string|null};
type ApiNotification = {id:string;category:NotificationCategory;title:string;description:string;receivedAt:string;read:boolean;accountId:string|null};
const mapItem=(item:ApiNotification):NotificationItem=>({...item,unread:!item.read,time:new Date(item.receivedAt).toLocaleDateString("pt-BR",{timeZone:"UTC"})});
const Context = createContext<{items:NotificationItem[];unreadCount:number;ready:boolean;error:string;reload:()=>void;isUnread:(id:string)=>boolean;markRead:(id:string)=>Promise<void>;markAllRead:()=>Promise<void>}|null>(null);
export function NotificationsProvider({children}:{children:ReactNode}) {
  const {authenticated}=useAuth();
  const [items,setItems]=useState<NotificationItem[]>([]);
  const [ready,setReady]=useState(false);
  const [error,setError]=useState("");
  const [attempt,setAttempt]=useState(0);
  useEffect(()=>{
    if(!authenticated)return;
    let active=true;
    const load=apiMode ? allPages<ApiNotification>("/notifications").then(data=>data.map(mapItem)) : demoStorage.read<string[]>("read-notifications").then(ids=>demoNotifications.map(item=>({...item,unread:item.unread && !(ids??[]).includes(item.id),accountId:item.id==="aluguel"?"aluguel":item.id==="pagamento"?"software":null})));
    load.then(data=>{if(active){setItems(data);setReady(true);setError("");}}).catch(reason=>{if(active){setReady(true);setError(reason instanceof Error?reason.message:"Falha nas notificações.");}});
    return()=>{active=false;};
  },[authenticated,attempt]);
  const markRead=useCallback(async(id:string)=>{
    if(apiMode)await request(`/notifications/${encodeURIComponent(id)}/read`,{method:"PATCH",body:JSON.stringify({read:true})});
    else await demoStorage.write("read-notifications",[...items.filter(x=>!x.unread).map(x=>x.id),id]);
    setItems(previous=>previous.map(x=>x.id===id?{...x,unread:false}:x));
  },[items]);
  async function markAllRead(){
    if(apiMode)await request("/notifications/read-all",{method:"POST"});
    else await demoStorage.write("read-notifications",items.map(x=>x.id));
    setItems(previous=>previous.map(x=>({...x,unread:false})));
  }
  return <Context.Provider value={{items,unreadCount:items.filter(x=>x.unread).length,ready,error,reload:()=>setAttempt(x=>x+1),isUnread:id=>Boolean(items.find(x=>x.id===id)?.unread),markRead,markAllRead}}>{children}</Context.Provider>;
}
export function useNotifications(){const value=useContext(Context);if(!value)throw new Error("NotificationsProvider ausente");return value;}
