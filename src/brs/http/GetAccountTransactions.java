package brs.http;

import brs.Account;
import brs.Burst;
import brs.BurstException;
import brs.Transaction;
import brs.db.BurstIterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetAccountTransactions extends APIServlet.APIRequestHandler {

  static final GetAccountTransactions instance = new GetAccountTransactions();

  private GetAccountTransactions() {
    super(new APITag[] {APITag.ACCOUNTS}, "account", "timestamp", "type", "subtype", "firstIndex", "lastIndex", "numberOfConfirmations");
  }

  @Override
  JSONStreamAware processRequest(HttpServletRequest req) throws BurstException {

    Account account = ParameterParser.getAccount(req);
    int timestamp = ParameterParser.getTimestamp(req);
    int numberOfConfirmations = ParameterParser.getNumberOfConfirmations(req);

    byte type;
    byte subtype;
    try {
      type = Byte.parseByte(req.getParameter("type"));
    } catch (NumberFormatException e) {
      type = -1;
    }
    try {
      subtype = Byte.parseByte(req.getParameter("subtype"));
    } catch (NumberFormatException e) {
      subtype = -1;
    }

    int firstIndex = ParameterParser.getFirstIndex(req);
    int lastIndex = ParameterParser.getLastIndex(req);

    JSONArray transactions = new JSONArray();
    try (BurstIterator<? extends Transaction> iterator = Burst.getBlockchain().getTransactions(account, numberOfConfirmations, type, subtype, timestamp,
                                                                                             firstIndex, lastIndex)) {
      while (iterator.hasNext()) {
        Transaction transaction = iterator.next();
        transactions.add(JSONData.transaction(transaction));
      }
    }

    JSONObject response = new JSONObject();
    response.put("transactions", transactions);
    return response;

  }

}
