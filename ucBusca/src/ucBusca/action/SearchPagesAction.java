package ucBusca.action;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;
import ucBusca.model.UcBuscaBean;

import java.lang.reflect.Array;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.*;

public class SearchPagesAction extends ActionSupport implements SessionAware {
    private static final long serialVersionUID = 5590830L;
    private Map<String, Object> session;
    private String url = null;

    @Override
    public String execute() throws Exception {
        String searchResult;
        String searchResultList[];
        int numberPages;

        if (this.url != null) {
            searchResult = this.getUcBuscaBean().getSearchPagesResults(this.url);

            searchResultList = searchResult.split(Pattern.quote("|"));

            numberPages = Integer.parseInt(searchResultList[0]);

            this.session.put("searchPagesResults", Arrays.copyOfRange(searchResultList, 1, searchResultList.length - 1));
            this.session.put("numberPages", numberPages);
        }

        else {
            return INPUT;
        }
        return SUCCESS;

    }

    public void setSearch(String url) {
        this.url = url; // will you sanitize this input? maybe use a prepared statement?
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
