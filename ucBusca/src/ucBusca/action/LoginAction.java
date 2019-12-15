package ucBusca.action;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;
import ucBusca.model.UcBuscaBean;

import java.rmi.RemoteException;
import java.util.Map;

/**
 * Class that relays to the bean the credentials of the user to login.
 * The session variables are then updated according to the users' permission level.
 * The bean's clientId is also updated to reflect the user's.
 */
public class LoginAction extends ActionSupport implements SessionAware {
    private static final long serialVersionUID = 5590830L;
    /**
     * The user's session.
     */
    private Map<String, Object> session;
    /**
     * The credentials of the user to log in.
     */
    private String username = null, password = null;

    /**
     * Execute: calls a method in the bean that returns the validation of the suer's credentials.
     * @return String - SUCCESS if the user exists, NONE if the user is not in the data base. ERROR if something went wrong in the bean and INPUT id the user failed to input credentials.
     * @throws Exception
     */
    @Override
    public String execute() throws Exception {
        if( (this.username != null && !username.equals("")) && (this.password != null && !password.equals("")) ) {
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
    public void setUsername(String username) {
        this.username = username; // will you sanitize this input? maybe use a prepared statement?
    }

    /**
     * Sets the user's password.
     * @param password String
     */
    public void setPassword(String password) {
        this.password = password; // what about this input?
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
     * Sets the user's bean.
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
