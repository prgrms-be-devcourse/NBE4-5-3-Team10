"use client";
import { Suspense } from "react";
import ClientPage from "./ClientPage";
import Header from "@/components/Header";
import Footer from "@/components/Footer";

export default function Page() {
  return (
    <>
      <Header />
      <Suspense fallback={<div>로딩 중...</div>}>
        <ClientPage />
      </Suspense>
      <Footer />
    </>
  );
}
