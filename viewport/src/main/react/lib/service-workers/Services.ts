export class Services {
    worker: ServiceWorkerContainer;

    constructor(url: string) {
        this.worker = navigator.serviceWorker;
        
        this.worker.register(url)
            .then(registration => {
                console.log(`Service Worker '${url}' is registered!`);
            })
            .catch((error) => console.error(`Service Worker: Error:\n ${error}`))
    }

    static areSupported = () => navigator.serviceWorker ? true : false;
}