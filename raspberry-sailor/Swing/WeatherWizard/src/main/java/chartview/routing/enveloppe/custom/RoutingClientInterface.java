package chartview.routing.enveloppe.custom;

import java.util.List;

public interface RoutingClientInterface
{
  public void routingNotification(List<List<RoutingPoint>> all, RoutingPoint closest);
  
}
