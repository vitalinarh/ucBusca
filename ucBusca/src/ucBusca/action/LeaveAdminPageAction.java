package ucBusca.action;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;
import ucBusca.model.UcBuscaBean;

import java.rmi.RemoteException;
import java.util.Map;

/**
 * Special action: called when the user leaves the admin page.
 * Calls a method in the bean that waits for the AdminPageUpdaterThread to die.
 */
public class LeaveAdminPageAction extends ActionSupport implements SessionAware {
    private static final long serialVersionUID = 5590830L;
    /**
     * The user's session.
     */
    private Map<String, Object> session;


    /**
     * Execute: calls a method in the bean that initiates the updater thread.
     * @return String - SUCCESS.
     * @throws Exception
     */
    @Override
    public String execute() throws Exception {

        this.getUcBuscaBean().setIsOnAdminPage();

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
     * Sets the user's bean, if there is no bean already.
     * @param ucBuscaBean
     */
    public void setUcBuscaBean(UcBuscaBean ucBuscaBean) {
        this.session.put("ucBuscaBean", ucBuscaBean);
    }

    /**
     * Creates the user's session, if there is no session already.
     * @param session
     */
    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }
}
