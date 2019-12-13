package ucBusca.action;

/**
 * class for link info that stores url and counts of times mentioned
 */
public class Page {

    /**
     * url link
     */
    public String url;
    /**
     * citation
     */
    public String citation;

    /**
     * title
     */
    public String title;

    /**
     * count
     */
    public int count;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    /**
     * constructor
     * @param url
     */
    public Page(String url, String citation, String title, int count) {
        this.url = url;
        this.citation = citation;
        this.title = title;
        this.count = count;
    }

    /**
     * gets url link
     * @return
     */
    public String getUrl() {
        return url;
    }

    /**
     * sets url link
     * @param url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    public String getCitation() {
        return citation;
    }

    public void setCitation(String citation) {
        this.citation = citation;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
