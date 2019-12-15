package ucBusca.action;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;
import ucBusca.model.UcBuscaBean;

import java.rmi.RemoteException;
import java.util.Map;

/**
 * Class that is responsible for relaying to the bean the url to be indexed.
 */
public class IndexURLAction extends ActionSupport implements SessionAware {
    private static final long serialVersionUID = 5590830L;
    /**
     * User's session.
     */
    private Map<String, Object> session;
    /**
     * The url to index.
     */
    private String urlToIndex = null;

    /**
     * Execute: if a url was inputed, it calls a method in the bean to relay it.
     * @return String - SUCCESS if the action resulted in no errors, INPUT if no url was given.
     * @throws Exception
     */
    @Override
    public String execute() throws Exception {
        if(this.urlToIndex != null){
            String response = this.getUcBuscaBean().getURLIndexed(this.urlToIndex);

            this.session.put("response", response);

            return SUCCESS;
        }

        return INPUT;
    }

    /**
     * Sets the url to index.
     * @param urlToIndex String - url to index.
     */
    public void setUrlToIndex(String urlToIndex) { this.urlToIndex = urlToIndex; }

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
     * Sets the user's bean, if there isn't one already.
     * @param ucBuscaBean
     */
    public void setUcBuscaBean(UcBuscaBean ucBuscaBean) {
        this.session.put("ucBuscaBean", ucBuscaBean);
    }

    /**
     * Sets the user's session, if there isn't one already.
     * @param session
     */
    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }
}
