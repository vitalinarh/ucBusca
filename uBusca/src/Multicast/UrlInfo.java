package Multicast;

/**
 * class for link info that stores url and counts of times mentioned
 */
public class UrlInfo {

    /**
     * url link
     */
    public String url;
    /**
     * count of references
     */
    public int count;

    /**
     * constructor
     * @param url
     * @param count
     */
    public UrlInfo(String url, int count) {
        this.url = url;
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

    @Override
    public String toString() {
        return "UrlInfo{" +
                "url='" + url + '\'' +
                ", count=" + count +
                '}';
    }

    /**
     * gets count of references
     * @return
     */
    public int getCount() {
        return count;
    }

    /**
     * sets count of references
     * @param count
     */
    public void setCount(int count) {
        this.count = count;
    }
}
