import List from "../components/List/list.jsx";
import Form from "../components/Form/form.jsx";
import styles from "./Recepti.module.css";

function Recepti() {
  return (
    <div className={styles.pageContainer}>
      <header className={styles.header}>
        <h1 className={styles.title}>Moja Kuhinja</h1>
        <p className={styles.subtitle}>Odkrijte, ustvarite in delite svoje najljubše recepte</p>
      </header>
      
      <div className={styles.contentWrapper}>
        <aside className={styles.sidebar}>
          <Form />
        </aside>
        <main className={styles.mainContent}>
          <List />
        </main>
      </div>
    </div>
  );
}

export default Recepti;
