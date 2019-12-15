package ucBusca.action;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;
import ucBusca.model.UcBuscaBean;

import java.rmi.RemoteException;
import java.util.Map;

/**
 * Class responsible for relaying to the bean the anonymous user's desired credentials.
 * After that, it calls the login method in the bean to automatically log the user in.
 */
public class RegisterAction extends ActionSupport implements SessionAware {
    private static final long serialVersionUID = 5590830L;
    /**
     * User's session.
     */
    private Map<String, Object> session;
    /**
     * User's desired credentials.
     */
    private String username = null, password = null;

    /**
     * Execute: calls the register and login methods in the bean.
     * It also updates the user's session variables according to its level of permissions.
     * @return String - ERROR if the user's credentials are already in use or if something went wrong in the bean, NONE if there are no user's with the credentials or INPUT if the user didn't give enough credentials.
     * @throws Exception
     */
    @Override
    public String execute() throws Exception {
        if( (this.username != null && !username.equals("")) && (this.password != null && !password.equals("")) ) {
            int registerStatus = this.getUcBuscaBean().getRegister(this.username, this.password);

            if(registerStatus == -1 | registerStatus == -2){
                return ERROR;
            }

            int loginStatus = this.getUcBuscaBean().getLogin(this.username, this.password);

            switch(loginStatus) {
                case (-1): //incorrect credentials
                    session.put("isLogged", false);
                    session.put("isAdmin", false);

                    return NONE;

                case (0): //standard user
                    session.put("isLogged", true);
                    session.put("isAdmin", false);

                    session.put("username", this.username);
                    session.put("userId", this.getUcBuscaBean().getClientId());

                    return SUCCESS;

                case (1): //admin user
                    session.put("isLogged", true);
                    session.put("isAdmin", true);

                    session.put("username", this.username);
                    session.put("userId", this.getUcBuscaBean().getClientId());

                    return SUCCESS;

                default: //qualquer outra situação
                    return ERROR;
            }
        }
        else {
            this.session.put("isLogged", false);
            return INPUT;
        }


    }

    /**
     * Sets the user's name.
     * @param username String
     */
    public void setUsername(String username) { this.username = username; }

    /**
     * Sets the user's password.
     * @param password String
     */
    public void setPassword(String password) { this.password = password; }

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
     * Sets the user's bean.
     * @param ucBuscaBean
     */
    public void setUcBuscaBean(UcBuscaBean ucBuscaBean) {
        this.session.put("ucBuscaBean", ucBuscaBean);
    }

    /**
     * Sets the suers session.
     * @param session
     */
    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }
}
