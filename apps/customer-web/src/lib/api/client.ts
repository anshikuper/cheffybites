import type { ApiError } from "@cheffybites/api-client";
import { z } from "zod";

const apiBaseUrl = z.string().url().parse(
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080/api/v1",
);

export const apiConfiguration = Object.freeze({ basePath: apiBaseUrl });

export type ApiFailure = ApiError;
