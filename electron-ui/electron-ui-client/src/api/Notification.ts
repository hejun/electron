import HttpClient from "@/api/HttpClient.ts";
import type Result from "@/api/Result.ts";

const http = new HttpClient(import.meta.env.VITE_REQUEST_BASE_URL)

export async function sendNotice() {
  return http.post<Result<string>>('/notification/notice', {})
}