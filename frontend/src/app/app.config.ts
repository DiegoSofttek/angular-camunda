import { provideBrowserGlobalErrorListeners } from "@angular/core";
import type { ApplicationConfig } from "@angular/core";
import { provideHttpClient } from "@angular/common/http";
import { provideRouter } from "@angular/router";
import Aura from "@primeuix/themes/aura";
import { providePrimeNG } from "primeng/config";
import { routes } from "./app.routes";

export const appConfig: ApplicationConfig = {
	providers: [
		provideBrowserGlobalErrorListeners(),
		provideHttpClient(),
		provideRouter(routes),
		providePrimeNG({
			theme: { preset: Aura },
			ripple: true,
		}),
	],
};
