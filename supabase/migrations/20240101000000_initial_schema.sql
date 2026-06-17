-- Create users table
CREATE TABLE public.users (
    id UUID REFERENCES auth.users ON DELETE CASCADE NOT NULL PRIMARY KEY,
    email TEXT,
    full_name TEXT,
    avatar_url TEXT,
    created_at BIGINT NOT NULL
);

-- Create groups table
CREATE TABLE public.groups (
    id UUID NOT NULL PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    cover_url TEXT,
    created_by UUID REFERENCES public.users(id) NOT NULL,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    deleted_at BIGINT
);

-- Create group_members table
CREATE TABLE public.group_members (
    id UUID NOT NULL PRIMARY KEY,
    group_id UUID REFERENCES public.groups(id) ON DELETE CASCADE NOT NULL,
    user_id UUID REFERENCES public.users(id) ON DELETE CASCADE NOT NULL,
    joined_at BIGINT NOT NULL,
    role TEXT NOT NULL DEFAULT 'MEMBER'
);

-- Create expenses table
CREATE TABLE public.expenses (
    id UUID NOT NULL PRIMARY KEY,
    group_id UUID REFERENCES public.groups(id) ON DELETE CASCADE NOT NULL,
    title TEXT NOT NULL,
    amount DOUBLE PRECISION NOT NULL,
    currency_code TEXT NOT NULL,
    date BIGINT NOT NULL,
    created_by UUID REFERENCES public.users(id) NOT NULL,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    deleted_at BIGINT,
    version INTEGER NOT NULL DEFAULT 1
);

-- Create expense_payers table
CREATE TABLE public.expense_payers (
    id UUID NOT NULL PRIMARY KEY,
    expense_id UUID REFERENCES public.expenses(id) ON DELETE CASCADE NOT NULL,
    user_id UUID REFERENCES public.users(id) NOT NULL,
    amount DOUBLE PRECISION NOT NULL
);

-- Create expense_splits table
CREATE TABLE public.expense_splits (
    id UUID NOT NULL PRIMARY KEY,
    expense_id UUID REFERENCES public.expenses(id) ON DELETE CASCADE NOT NULL,
    user_id UUID REFERENCES public.users(id) NOT NULL,
    amount DOUBLE PRECISION NOT NULL
);

-- Enable Row Level Security
ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.groups ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.group_members ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.expenses ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.expense_payers ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.expense_splits ENABLE ROW LEVEL SECURITY;

-- Basic Policies for development (auth required)
CREATE POLICY "Allow authenticated users to read users" ON public.users FOR SELECT USING (auth.role() = 'authenticated');
CREATE POLICY "Allow users to insert themselves" ON public.users FOR INSERT WITH CHECK (auth.uid() = id);
CREATE POLICY "Allow users to update themselves" ON public.users FOR UPDATE USING (auth.uid() = id);

-- Dev mode policies: allow authenticated users full access to other tables
CREATE POLICY "Allow all for authenticated on groups" ON public.groups FOR ALL USING (auth.role() = 'authenticated');
CREATE POLICY "Allow all for authenticated on group_members" ON public.group_members FOR ALL USING (auth.role() = 'authenticated');
CREATE POLICY "Allow all for authenticated on expenses" ON public.expenses FOR ALL USING (auth.role() = 'authenticated');
CREATE POLICY "Allow all for authenticated on expense_payers" ON public.expense_payers FOR ALL USING (auth.role() = 'authenticated');
CREATE POLICY "Allow all for authenticated on expense_splits" ON public.expense_splits FOR ALL USING (auth.role() = 'authenticated');
