package ucBusca.action;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;
import ucBusca.model.UcBuscaBean;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Class responsible for relaying to the bean the search query given by the user.
 * It parses the bean's response and builds Page objects to hold the results' information.
 *
 */
public class SearchAction extends ActionSupport implements SessionAware {
    private static final long serialVersionUID = 5590830L;
    /**
     * User's session.
     */
    private Map<String, Object> session;
    /**
     * Search query given by the user.
     */
    private String search = null;
    private static final String SAMPLE_KEY = "trnsl.1.1.20191214T152639Z.763aac1b6a3b7865.b01c2b569456e48f6058fd02c10d4f1dd7c84c3f";
    private ArrayList<Page> pagesList = new ArrayList<>();

    @Override
    public String execute() throws Exception {
        String searchResult, title, url, citation;
        String searchResultList[];
        int numResults, numReferences, i = 1;

        session.put("translate", "false");

        if (this.search != null) {

            searchResult = this.getUcBuscaBean().getSearchResults(this.search);

            searchResultList = searchResult.split(Pattern.quote("|"));

            numResults = Integer.parseInt(searchResultList[0]);


            while(i + 3 <= searchResultList.length) {

                title = searchResultList[i];
                i++;
                url = searchResultList[i];
                i++;
                numReferences = Integer.parseInt(searchResultList[i]);
                i++;
                citation = searchResultList[i];
                String lang = getUcBuscaBean().getLanguage(citation);
                String translation = getUcBuscaBean().getTranslation(citation, lang);
                i++;
                Page newPage = new Page(url, citation, title, numReferences, lang, translation);
                pagesList.add(newPage);
            }

            this.session.put("searchResults", pagesList);
            this.session.put("numResults", numResults);
        }

        else {
            return INPUT;
        }
        return SUCCESS;

    }

    public void setSearch(String search) {
        this.search = search; // will you sanitize this input? maybe use a prepared statement?
    }

    public UcBuscaBean getUcBuscaBean() throws RemoteException {
        if(!session.containsKey("ucBuscaBean"))
            this.setUcBuscaBean(new UcBuscaBean());
        return (UcBuscaBean) session.get("ucBuscaBean");
    }

    public void setUcBuscaBean(UcBuscaBean ucBuscaBean) {
        this.session.put("ucBuscaBean", ucBuscaBean);
    }

    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }
}
