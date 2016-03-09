import org.crsh.cli.Command
import org.jasig.cas.support.oauth.token.RefreshTokenImpl;

class oauth {

    @Command
    public String export() {
        def entityManagerFactory = context.attributes.beans["entityManagerFactory"];
        def entityManager = entityManagerFactory.createEntityManager();

        def tx = entityManager.getTransaction();
        tx.begin();
        result = entityManager.createQuery("select t from RefreshTokenImpl t", RefreshTokenImpl.class).getResultList();
        tx.commit();

        return result.toString();
    }
}
