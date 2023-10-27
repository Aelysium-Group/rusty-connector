export class Body {
    contents: Map<string, any> = new Map();

    constructor() {}

    /**
     * Add a new attribute to the Body
     * @param key The attribute key
     * @param value The attribute value
     * @returns The new contents
     */
    insert = (key: string, value: any): Map<string, any> => this.contents.set(key,value);

    /**
     * Remove an attribute from the Body
     * @param key The attribute key
     * @returns A boolean indicating success
     */
    drop = (key: string, value: any): boolean => this.contents.delete(key);

    /**
     * Parses the body to prepare it for transmission
     */
    get parse (): Map<string, any> { return this.contents; }
}