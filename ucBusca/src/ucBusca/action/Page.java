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

    /**
     * language
     */
    public String language;

    public String translation;

    public String title_translation;

    boolean showTranslation = false;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    /**
     * constructor
     * @param url
     */
    public Page(String url, String citation, String title, int count, String language, String translation, String title_translation) {
        this.url = url;
        this.citation = citation;
        this.title = title;
        this.count = count;
        this.language = language;
        this.translation = translation;
        this.title_translation = title_translation;
    }

    public String getTitle_translation() {
        return title_translation;
    }

    public void setTitle_translation(String title_translation) {
        this.title_translation = title_translation;
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
