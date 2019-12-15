package ucBusca.action;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;
import ucBusca.model.UcBuscaBean;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Class responsible for showing the user their search history and putting it in their session.
 */
public class SearchHistoryAction extends ActionSupport implements SessionAware {
    private static final long serialVersionUID = 5590830L;
    /**
     * User's session.
     */
    private Map<String, Object> session;

    /**
     * Execute: calls a method in the bean that returns the user's search history.
     * It then parses it and puts it in a session variable.
     * @return String - SUCCESS.
     * @throws Exception
     */
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

    /**
     * Gets the user's bean.
     * @return
     * @throws RemoteException
     */
    public UcBuscaBean getUcBuscaBean() throws RemoteException {
        if(!session.containsKey("ucBuscaBean"))
            this.setUcBuscaBean(new UcBuscaBean());
        return (UcBuscaBean) session.get("ucBuscaBean");
    }

    /**
     * Sets the user's bean
     * @param ucBuscaBean
     */
    public void setUcBuscaBean(UcBuscaBean ucBuscaBean) {
        this.session.put("ucBuscaBean", ucBuscaBean);
    }

    /**
     * Sets the user's session.
     * @param session
     */
    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }

}
