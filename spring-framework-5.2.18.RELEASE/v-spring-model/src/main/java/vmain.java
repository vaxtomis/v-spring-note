import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * @author vaxtomis
 */
public class vmain {
	public static void main(String[] args) {
		BeanFactory container = new XmlBeanFactory(new ClassPathResource("spring-config.xml"));
		MockNewsPersister persister = (MockNewsPersister)container.getBean("mockPersister");
		persister.persistNews();
		persister.persistNews();
	}
}
