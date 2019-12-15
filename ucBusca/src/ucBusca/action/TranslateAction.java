package ucBusca.action;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;
import ucBusca.model.UcBuscaBean;

import java.rmi.RemoteException;
import java.util.Map;

import static com.opensymphony.xwork2.Action.SUCCESS;

public class TranslateAction extends ActionSupport implements SessionAware {

    private static final long serialVersionUID = 5590830L;
    private Map<String, Object> session;

    @Override
    public String execute() throws Exception {

        System.out.println(session.get("translate"));
        if(session.get("translate").equals("true")){
            session.put("translate", "false");
        }
        else{
            session.put("translate", "true");
        }

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
