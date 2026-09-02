import { Layout } from '../routes/Layout';

export function ComingSoonPage({ title }: { title: string }) {
  return (
    <Layout title={title}>
      <section className="card">
        <p>Тази секция е в процес на разработка.</p>
      </section>
    </Layout>
  );
}
