package ucBusca.action;

import Server.UrlInfo;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;
import ucBusca.model.UcBuscaBean;

import java.lang.reflect.Array;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Pattern;

public class SearchAction extends ActionSupport implements SessionAware {
    private static final long serialVersionUID = 5590830L;
    private Map<String, Object> session;
    private String search = null;
    private ArrayList<Page> pagesList = new ArrayList<>();

    @Override
    public String execute() throws Exception {
        String searchResult, title, url, citation;
        String searchResultList[];
        int numResults, numReferences, i = 1;

        if (this.search != null) {

            searchResult = this.getUcBuscaBean().getSearchResults(this.search);

            searchResultList = searchResult.split(Pattern.quote("|"));

            numResults = Integer.parseInt(searchResultList[0]);

            while(i + 4 <= searchResultList.length) {
                title = searchResultList[i];
                i++;
                url = searchResultList[i];
                i++;
                numReferences = Integer.parseInt(searchResultList[i]);
                i++;
                citation = searchResultList[i];
                i++;
                Page newPage = new Page(url, citation, title, numReferences);
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
