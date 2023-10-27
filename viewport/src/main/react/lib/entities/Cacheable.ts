export class Cacheable {
    constructor() {
        if (this.constructor == Cacheable) throw new Error("You cannot define instance of abstract class CacheModule!");
    }
}