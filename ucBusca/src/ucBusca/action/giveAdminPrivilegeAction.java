package ucBusca.action;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;
import ucBusca.model.UcBuscaBean;

import java.rmi.RemoteException;
import java.util.Map;

/**
 * Class that will be responsible for relaying to the bean the user to admin.
 * Also cheanges the view to the admin page.
 */
public class giveAdminPrivilegeAction extends ActionSupport implements SessionAware {
    private static final long serialVersionUID = 5590830L;
    /**
     * User's session.
     */
    private Map<String, Object> session;
    /**
     * The name of the user to give admin privileges.
     */
    private String userToAdmin = null;

    /**
     * Execute: if a username has been inputed, calls a method in the bean to relay it.
     * @return String - SUCCESS if the user existed, NONE, if there was no user with the given name, ERROR if the operation failed, INPUT if no name was inputed.
     * @throws Exception
     */
    @Override
    public String execute() throws Exception {
        if(this.userToAdmin != null){
            int validation = this.getUcBuscaBean().getUserAdmined(this.userToAdmin);

            if(validation == 1)
                return SUCCESS;
            else if(validation == -2)
                return ERROR;
            else
                return NONE;

        }

        return INPUT;
    }

    /**
     * Sets the user to admin's name.
     * @param userToAdmin String - user to admin.
     */
    public void setUserToAdmin(String userToAdmin) { this.userToAdmin = userToAdmin; }

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
