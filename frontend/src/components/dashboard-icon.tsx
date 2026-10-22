import type { ReactNode } from "react";

const drawings = {
  attachment: <path d="m9 17 8-8a3 3 0 0 0-4-4L4 14a5 5 0 0 0 7 7L21 11a7 7 0 0 0-10-10L3 9M7 15l8-8" />,
  back: <path d="m14 7-5 5 5 5M9 12h11" />,
  check: <path d="m5 12 4 4L19 6" />,
  search: <><circle cx="10.5" cy="10.5" r="5.5" /><path d="m15 15 4 4" /></>,
  bell: <><path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9M10 21h4" /></>,
  eye: <><path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12Z" /><circle cx="12" cy="12" r="3" /></>,
  hidden: <><path d="m3 3 18 18M10.5 5.1 12 5c6.5 0 10 7 10 7a20 20 0 0 1-3 3.7M6.2 6.2A22 22 0 0 0 2 12s3.5 7 10 7a12 12 0 0 0 5.6-1.4M10 10a3 3 0 0 0 4 4" /></>,
  down: <path d="M12 4v16m-6-6 6 6 6-6" />,
  up: <path d="M12 20V4m-6 6 6-6 6 6" />,
  exchange: <path d="M4 7h15m-4-4 4 4-4 4M20 17H5m4-4-4 4 4 4" />,
  clock: <><circle cx="12" cy="12" r="9" /><path d="M12 7v5l3 2" /></>,
  home: <><path d="m3 10 9-7 9 7v11H3V10Z" /><path d="M9 21v-8h6v8" /></>,
  wallet: <><rect x="3" y="6" width="18" height="15" rx="2" /><path d="M3 9V5a2 2 0 0 1 2-2h13v3M16 12h5v5h-5a2.5 2.5 0 0 1 0-5Z" /></>,
  document: <><path d="M6 2h8l5 5v15H6V2Z" /><path d="M14 2v6h5M9 13h7M9 17h7" /></>,
  user: <><circle cx="12" cy="7" r="4" /><path d="M4 22v-2a8 8 0 0 1 16 0v2" /></>,
  plus: <path d="M12 4v16M4 12h16" />,
} satisfies Record<string, ReactNode>;

export type DashboardIconName = keyof typeof drawings;
export function DashboardIcon({ name }: { name: DashboardIconName }) {
  return <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">{drawings[name]}</svg>;
}
