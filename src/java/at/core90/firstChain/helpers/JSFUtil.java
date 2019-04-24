package at.core90.firstChain.helpers;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

public class JSFUtil {

  public static void displayInfo(String msg) {
    displayMessage(FacesMessage.SEVERITY_INFO, msg);
  }

  public static void displayWarning(String msg) {
    displayMessage(FacesMessage.SEVERITY_WARN, msg);
  }

  public static void displayMessage(FacesMessage.Severity type, String msg) {
    FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(type, msg, msg));
  }

}
