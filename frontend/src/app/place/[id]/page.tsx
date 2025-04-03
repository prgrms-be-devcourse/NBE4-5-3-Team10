import { Suspense } from "react";
import Header from "@/components/Header";
import Footer from "@/components/Footer";
import ClientPage from "./ClientPage";

export default async function Page() {
  return (
    <div className="min-h-screen flex flex-col">
      <Header />
      <main className="flex-grow">
        <Suspense fallback={<div>로딩 중...</div>}>
          <ClientPage />
        </Suspense>
      </main>
      <Footer />
    </div>
  );
}
