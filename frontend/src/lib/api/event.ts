import axios from "@/lib/axios"; 

export const fetchAllEvents = async () => {
  const res = await axios.get("/admin/event");
  return res.data;
};

export const createEvent = async (payload: {
  title: string;
  description: string;
  eventDate: string;
}) => {
  const res = await axios.post("/admin/event", payload);
  return res.data;
};

export const deleteEvent = async (id: number) => {
  const res = await axios.delete(`/admin/event/${id}`);
  return res.data;
};

export const updateEvent = async (
  id: number,
  payload: { title: string; description: string; eventDate: string }
) => {
  const res = await axios.put(`/admin/event/${id}`, payload);
  return res.data;
};
