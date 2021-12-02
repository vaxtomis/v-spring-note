/**
 * @author vaxtomis
 */
public class MockNewsPersister {
	private NewsBean newsBean;

	public void persistNews(NewsBean bean) {
		persistNews();
	}

	public void persistNews() {
		System.out.println("Persist bean: " + getNewsBean());
	}

	public NewsBean getNewsBean() {
		return newsBean;
	}

	public void setNewsBean(NewsBean newsBean) {
		this.newsBean = newsBean;
	}
}
