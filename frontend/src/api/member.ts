import api from "@/lib/api";

export type Member = {
  id: number;
  username: string;
  email: string;
  nickname: string;
};

export const fetchAllMembers = async (): Promise<Member[]> => {
  const res = await api.get("/member/all"); 
  return res.data;
};
