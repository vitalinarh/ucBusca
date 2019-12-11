package ucBusca.action;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;
import ucBusca.model.UcBuscaBean;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Map;

public class SearchAction extends ActionSupport implements SessionAware {
    private static final long serialVersionUID = 5590830L;
    private Map<String, Object> session;
    private String search = null;

    @Override
    public String execute() throws Exception {
        String searchResult;
        ArrayList<String> searchResultList;

        if (this.search != null) {
            searchResult = this.getUcBuscaBean().getSearchResults(this.search);
            this.session.put("searchResults", searchResult);
        }

        else {
            return INPUT;
        }
        return SUCCESS;

    }

    public void setSearch(String search) {
        this.search = search; // will you sanitize this input? maybe use a prepared statement?
    }

    public UcBuscaBean getUcBuscaBean() {
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