package ucBusca.action;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;
import ucBusca.model.UcBuscaBean;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.regex.Pattern;

public class SearchHistoryAction extends ActionSupport implements SessionAware {
    private static final long serialVersionUID = 5590830L;
    private Map<String, Object> session;

    @Override
    public String execute() throws Exception {

        String searchHistoryList[];

        String searchHistory = this.getUcBuscaBean().getSearchHistory();

        searchHistoryList = searchHistory.split(Pattern.quote("|"));

        if(searchHistory == null)
            return ERROR;

        this.session.put("searchHistory", searchHistoryList);

        return SUCCESS;

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
