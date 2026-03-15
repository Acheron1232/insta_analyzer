export interface AnalysisResult {
  overallScore: number;
  smallBusiness: boolean;
  ukraineBased: boolean;
  lacksWebsite: boolean;
  lacksCrm: boolean;
  lacksTelegramBot: boolean;
  sellsViaDms: boolean;
  active: boolean;
  detectedCategory: string;
  reasoning: string;
  aiProvider: string;
  analyzedAt: string;
}

export interface Profile {
  id: number;
  username: string;
  fullName: string;
  biography: string;
  externalUrl: string | null;
  websiteDomain: string | null;
  contactPhone: string | null;
  telegramHandle: string | null;
  profilePicUrl: string | null;
  followerCount: number;
  mediaCount: number;
  businessAccount: boolean;
  category: string | null;
  dataSource: string;
  fetchedAt: string;
  analysis: AnalysisResult | null;
}

export interface ScanJob {
  id: number;
  searchQuery: string;
  dataSource: string;
  aiProvider: string;
  status: string;
  profilesFound: number;
  profilesAnalyzed: number;
  leadsFound: number;
  startedAt: string;
  completedAt: string | null;
  errorMessage: string | null;
}

export interface DashboardStats {
  totalProfiles: number;
  totalLeads: number;
  avgScore: number;
  totalScans: number;
  runningScans: number;
}

export interface ScanRequest {
  query: string;
  dataSource: 'INSTAGRAM4J' | 'APIFY' | 'RAPIDAPI';
  aiProvider: 'OLLAMA' | 'GEMINI';
  limit: number;
}

export interface UkraineScanRequest {
  dataSource: 'INSTAGRAM4J' | 'APIFY' | 'RAPIDAPI';
  aiProvider: 'OLLAMA' | 'GEMINI';
  limit: number;
  niches?: string[];
  cities?: string[];
}

export interface AnalyzeRequest {
  username: string;
  dataSource: 'INSTAGRAM4J' | 'APIFY' | 'RAPIDAPI';
  aiProvider: 'OLLAMA' | 'GEMINI';
}
