package ucBusca.action;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;
import ucBusca.model.UcBuscaBean;

import java.rmi.RemoteException;
import java.util.Map;

public class giveAdminPrivilegeAction extends ActionSupport implements SessionAware {
    private static final long serialVersionUID = 5590830L;
    private Map<String, Object> session;
    private String userToAdmin = null;

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

    public void setUserToAdmin(String userToAdmin) { this.userToAdmin = userToAdmin; }

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
