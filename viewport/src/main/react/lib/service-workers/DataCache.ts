const cacheName = 'data_cache';

const init = () => {

    }

const install = (event: any) => {
        console.log("Service Worker: DataCache has been installed.");

        event.waitUntil(
            caches.open(cacheName)
                .then((cache) => {

                })
                .then(() => event.skipWaiting())
        );
    }

const activate = (event: Event) => {
        console.log("Service Worker: DataCache has been activated.");

        // Delete unwanted caches
        caches.keys()
            .then((names) => {
                return Promise.all(
                    names.map(currentCache => {
                        if(currentCache == cacheName) return;
                        return caches.delete(currentCache);
                    })
                )
            });
    }

const interceptor = (event: Event) => {
        
    }

self.addEventListener('install',        (event) => install(event));
self.addEventListener('activate',       (event) => activate(event));