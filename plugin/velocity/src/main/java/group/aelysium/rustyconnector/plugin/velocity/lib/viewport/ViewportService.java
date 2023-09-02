package group.aelysium.rustyconnector.plugin.velocity.lib.viewport;

import group.aelysium.rustyconnector.core.lib.connectors.storage.StorageConnector;
import group.aelysium.rustyconnector.core.lib.serviceable.ServiceableService;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.micro_services.gateway.GatewayService;

public class ViewportService extends ServiceableService<ViewportServiceHandler> {
    protected ViewportService(ViewportServiceHandler services) {
        super(services);
    }

    public static class Builder {
        protected final ViewportServiceHandler services = new ViewportServiceHandler();

        public Builder(){}

        public ViewportService.Builder setGatewayService(GatewayService gatewayService) {
            this.services.add(gatewayService);
            return this;
        }

        public ViewportService.Builder setStorageConnector(StorageConnector storageConnector) {
            this.services.add(storageConnector);
            return this;
        }


        public ViewportService build(){
            // If any of these aren't set, it will throw an exception.
            this.services.gatewayService();
            this.services.mySQLService();

            return new ViewportService(this.services);
        }

    }
}
