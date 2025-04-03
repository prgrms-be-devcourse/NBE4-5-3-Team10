import api from "@/lib/api";

export type Blacklist = {
  id: number;
  member: {
    id: number;
    username: string;
    email: string;
    nickname: string;
  };
  reason: string;
};

export const fetchBlacklist = async (): Promise<Blacklist[]> => {
  const res = await api.get("/admin/blacklist");
  return res.data;
};

export const addToBlacklist = async (memberId: number, reason: string) => {
  await api.post("/admin/blacklist", { memberId, reason });
};

export const removeFromBlacklist = async (memberId: number) => {
  await api.delete(`/admin/blacklist/${memberId}`);
};
