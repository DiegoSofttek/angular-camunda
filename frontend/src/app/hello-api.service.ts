import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import type { Observable } from 'rxjs';

/** Same path the WAR exposes when deployed as camunda-starter-template.war */
const API_BASE = '/camunda-starter-template/api';

export interface HelloResponse {
  greeting: string;
}

@Injectable({
  providedIn: 'root',
})
export class HelloApiService {
  private readonly http = inject(HttpClient);

  hello(name: string): Observable<HelloResponse> {
    const trimmed = name.trim();
    let params = new HttpParams();
    if (trimmed.length > 0) {
      params = params.set('name', trimmed);
    }
    return this.http.get<HelloResponse>(`${API_BASE}/hello`, { params });
  }
}
