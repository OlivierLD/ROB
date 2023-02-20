package chartview.routing.enveloppe.custom;

import java.util.List;

public interface RoutingClientInterface {
    void routingNotification(List<List<RoutingPoint>> all, RoutingPoint closest);
}
