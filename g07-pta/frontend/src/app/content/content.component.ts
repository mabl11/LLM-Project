import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  inject,
  Injectable,
  ViewChild,
  AfterViewChecked
} from '@angular/core';
import {FormControl} from '@angular/forms';
import {HttpClient} from '@angular/common/http';
import {CdkVirtualScrollViewport} from '@angular/cdk/scrolling';
import {MatSnackBar} from '@angular/material/snack-bar';

interface SourceDoc {
  name: string;
  id: string;
  path: string;
}

interface Message {
  text: string;
  sources?: SourceDoc[];
  role?: string;
  isUser: boolean;
}

@Component({
  selector: 'app-content',
  standalone: false,
  templateUrl: './content.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  styleUrl: './content.component.scss'
})
@Injectable({providedIn: 'root'})
export class ContentComponent implements AfterViewChecked{
  @Input() userRole: string = 'default';
  discussion: Message[] = [];
  input = new FormControl('');
  private http = inject(HttpClient);
  private cdr = inject(ChangeDetectorRef);
  private snackBar = inject(MatSnackBar);

  @ViewChild(CdkVirtualScrollViewport) viewport!: CdkVirtualScrollViewport;

  ngAfterViewChecked() {
    this.scrollToBottom();
  }

  onSubmit() {
    let input = this.input.value;
    if(!input) return;

    this.discussion = this.discussion.concat({
      text: input,
      sources: [],
      isUser: true
    });

   const jsonInput = {
     request: input,
     userRole: this.userRole
   };

    this.http.post<any>('/api/request', jsonInput).subscribe((res: any) => {
      this.discussion = this.discussion.concat({
        text: res.answer,
        sources: res.sources,
        role: res.userRole,
        isUser: false
      });
      this.cdr.markForCheck();
    });

    this.input.setValue('');
  }

  copyPath(path: string): void {
    navigator.clipboard.writeText(path).then(() => {
      this.snackBar.open('Pfad in die Zwischenablage kopiert!', 'OK', {
        duration: 3000,
        horizontalPosition: 'center',
        verticalPosition: 'bottom',
      });

    }).catch(err => {
      console.error('Kopieren fehlgeschlagen', err);
    });
  }

  private scrollToBottom() {
    if (this.viewport) {
      this.viewport.scrollToIndex(this.discussion.length - 1, 'smooth');
    }
  }
}
