import { describe, expect, it } from "vitest";
import { nameToColor } from "./NameToColor.ts";

describe("nameToColor", () => {
    it("returnsDeterministicHexForSameName", () => {
        expect(nameToColor("alice")).toBe(nameToColor("alice"));
    });

    it("returnsDifferentColorsForDifferentNames", () => {
        expect(nameToColor("alice")).not.toBe(nameToColor("bob"));
    });
});
