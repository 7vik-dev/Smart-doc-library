import React, { useState } from 'react';
import { PlusCircle, Save } from 'lucide-react';

interface AddDocumentFormProps {
  onCreate: (title: string, content: string) => void;
  isLoading: boolean;
}

const AddDocumentForm: React.FC<AddDocumentFormProps> = ({ onCreate, isLoading }) => {
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim()) return;
    onCreate(title, content);
    setTitle('');
    setContent('');
  };

  return (
    <div className="card">
      <div className="card-header">
        <span className="card-title"><PlusCircle className="w-4 h-4" /> Add Document</span>
      </div>
      <div className="card-body">
        <form onSubmit={handleSubmit}>
          <div className="field">
            <label>Title</label>
            <input 
              type="text" 
              value={title} 
              onChange={(e) => setTitle(e.target.value)} 
              placeholder="e.g. Spring Boot Performance Guide" 
            />
          </div>
          <div className="field">
            <label>Content (optional)</label>
            <textarea 
              value={content} 
              onChange={(e) => setContent(e.target.value)} 
              placeholder="Paste document text here..." 
            />
          </div>
          <button className="btn btn-primary" type="submit" disabled={isLoading || !title.trim()}>
            {isLoading ? <span className="spinner" style={{ marginRight: '8px' }} /> : <Save className="w-4 h-4" style={{ marginRight: '8px' }} />}
            Save to MongoDB
          </button>
        </form>
      </div>
    </div>
  );
};

export default AddDocumentForm;
