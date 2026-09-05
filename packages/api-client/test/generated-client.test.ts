import { describe, expect, it } from "vitest";
import { ApiErrorFromJSON, ApiErrorToJSON } from "../src";

describe("generated API client", () => {
  it("round-trips the canonical safe error shape", () => {
    const error = ApiErrorFromJSON({
      code: "VALIDATION_FAILED",
      message: "The request contains invalid values.",
      traceId: "correlation-123",
      details: { field: "invalid" },
    });

    expect(ApiErrorToJSON(error)).toEqual({
      code: "VALIDATION_FAILED",
      message: "The request contains invalid values.",
      traceId: "correlation-123",
      details: { field: "invalid" },
    });
  });
});
