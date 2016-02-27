package com.xmpp.chat.data.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.widget.Toast;

public class ErrorDialog
  extends Dialog
{
  private ErrorDialog(Context paramContext)
  {
    super(paramContext);
  }
  
  public static Dialog show(Context paramContext, String paramString)
  {
    AlertDialog.Builder localBuilder = new AlertDialog.Builder(paramContext);
    localBuilder.setTitle("Error").setMessage(paramString).setPositiveButton("OK", null).setCancelable(false);
    AlertDialog localAlertDialog = localBuilder.create();
    localAlertDialog.show();
    return localAlertDialog;
  }
   
  public static void showToast(Context paramContext, String paramString)
  {
    Toast.makeText(paramContext, paramString, 0).show();
  }
}

