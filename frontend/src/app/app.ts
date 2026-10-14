import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterOutlet } from '@angular/router';
import { Button } from 'primeng/button';
import { Card } from 'primeng/card';
import { InputText } from 'primeng/inputtext';
import { HelloApiService } from './hello-api.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Card, InputText, Button, FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class App {
  private readonly helloApi = inject(HelloApiService);

  protected readonly name = signal('');
  protected readonly greeting = signal<string | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly loading = signal(false);

  protected greet(): void {
    this.error.set(null);
    this.loading.set(true);
    this.helloApi.hello(this.name()).subscribe({
      next: (res) => {
        this.greeting.set(res.greeting);
        this.loading.set(false);
      },
      error: (err: { error?: { error?: string } }) => {
        const msg =
          typeof err?.error?.error === 'string'
            ? err.error.error
            : 'Could not reach the backend. Is Camunda running on port 8080?';
        this.error.set(msg);
        this.loading.set(false);
      },
    });
  }
}
