<script>
	import { auth, isAuthenticated, currentUser, getRoleDashboard } from '$lib/stores/auth.js';
	import { formatRole } from '$lib/utils/formatters.js';
	import { page } from '$app/state';

	let mobileMenuOpen = $state(false);

	const activePath = $derived(page.url.pathname);
	const user = $derived($currentUser);
	const loggedIn = $derived($isAuthenticated);

	const dashboardUrl = $derived(user?.role ? getRoleDashboard(user.role) : '/dashboard/youth');

	function toggleMobileMenu() {
		mobileMenuOpen = !mobileMenuOpen;
	}

	function closeMobileMenu() {
		mobileMenuOpen = false;
	}

	function handleLogout() {
		closeMobileMenu();
		auth.logout();
	}
</script>

<header class="sticky top-0 z-40 w-full border-b border-slate-200/80 bg-white/90 backdrop-blur-md transition-all">
	<div class="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
		<!-- Brand / Logo -->
		<div class="flex items-center gap-3">
			<a href="/" class="flex items-center gap-2.5 group" onclick={closeMobileMenu}>
				<!-- Stylized Logo Mark with South African Flag Colors Accent -->
				<div class="relative flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-indigo-600 to-indigo-800 text-white font-black text-lg shadow-md shadow-indigo-500/20 group-hover:scale-105 transition-transform">
					<span>SSH</span>
					<div class="absolute -bottom-1 -right-1 flex h-3.5 w-3.5 items-center justify-center rounded-full bg-emerald-500 ring-2 ring-white">
						<span class="h-1.5 w-1.5 rounded-full bg-amber-300"></span>
					</div>
				</div>
				<div>
					<div class="flex items-center gap-1.5">
						<span class="text-base font-extrabold tracking-tight text-slate-900">Shared Services Hub</span>
						<span class="rounded bg-emerald-100 px-1.5 py-0.5 text-[10px] font-bold text-emerald-800 uppercase tracking-wide">ZA</span>
					</div>
					<p class="text-[11px] font-medium text-slate-500 hidden sm:block">Work. Income. Experience. Guaranteed.</p>
				</div>
			</a>
		</div>

		<!-- Desktop Navigation Links -->
		<nav class="hidden md:flex items-center gap-1 lg:gap-2">
			<a
				href="/"
				class="rounded-lg px-3 py-2 text-sm font-medium transition-colors {activePath === '/' ? 'text-indigo-600 bg-indigo-50/60 font-semibold' : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100/70'}"
			>
				Home
			</a>

			<a
				href="/tasks"
				class="rounded-lg px-3 py-2 text-sm font-medium transition-colors {activePath.startsWith('/tasks') ? 'text-indigo-600 bg-indigo-50/60 font-semibold' : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100/70'}"
			>
				Browse Tasks
			</a>

			{#if loggedIn}
				<a
					href={dashboardUrl}
					class="rounded-lg px-3 py-2 text-sm font-medium transition-colors {activePath.startsWith('/dashboard') ? 'text-indigo-600 bg-indigo-50/60 font-semibold' : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100/70'}"
				>
					My Dashboard
				</a>
			{/if}
		</nav>

		<!-- Right Side: User Profile / Auth Action Buttons -->
		<div class="hidden md:flex items-center gap-3">
			{#if loggedIn && user}
				<div class="flex items-center gap-3 pl-3 border-l border-slate-200">
					<div class="text-right">
						<div class="text-sm font-bold text-slate-900 leading-none">{user.name || user.email}</div>
						<div class="mt-1">
							<span class="inline-flex items-center rounded-md bg-indigo-50 px-2 py-0.5 text-[11px] font-semibold text-indigo-700 ring-1 ring-inset ring-indigo-700/10">
								{formatRole(user.role)}
							</span>
						</div>
					</div>

					<a
						href={dashboardUrl}
						class="flex h-9 w-9 items-center justify-center rounded-full bg-indigo-100 text-indigo-700 font-bold text-sm ring-2 ring-indigo-600/20 hover:ring-indigo-600 transition-all"
						title="Go to Dashboard"
					>
						{(user.name || 'U').charAt(0).toUpperCase()}
					</a>

					<button
						type="button"
						class="inline-flex items-center justify-center rounded-lg border border-slate-200 bg-white px-3 py-1.5 text-xs font-semibold text-slate-600 shadow-xs hover:bg-slate-50 hover:text-rose-600 hover:border-rose-200 transition-colors"
						onclick={handleLogout}
					>
						Logout
					</button>
				</div>
			{:else}
				<div class="flex items-center gap-2">
					<a
						href="/login"
						class="rounded-lg px-4 py-2 text-sm font-semibold text-slate-700 hover:text-indigo-600 hover:bg-slate-100 transition-colors"
					>
						Log In
					</a>
					<a
						href="/register"
						class="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white shadow-sm hover:bg-indigo-700 transition-all hover:shadow-md hover:shadow-indigo-500/20 active:scale-95"
					>
						Get Started
					</a>
				</div>
			{/if}
		</div>

		<!-- Mobile Hamburger Button -->
		<div class="flex md:hidden">
			<button
				type="button"
				class="inline-flex items-center justify-center rounded-lg p-2 text-slate-600 hover:bg-slate-100 hover:text-slate-900 focus:outline-none focus:ring-2 focus:ring-indigo-500"
				onclick={toggleMobileMenu}
				aria-expanded={mobileMenuOpen}
				aria-label="Toggle navigation menu"
			>
				{#if mobileMenuOpen}
					<svg class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
						<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
					</svg>
				{:else}
					<svg class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
						<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
					</svg>
				{/if}
			</button>
		</div>
	</div>

	<!-- Mobile Dropdown Menu -->
	{#if mobileMenuOpen}
		<div class="md:hidden border-b border-slate-200 bg-white px-4 pt-2 pb-6 space-y-3 animate-in slide-in-from-top-2 duration-150">
			<nav class="space-y-1">
				<a
					href="/"
					class="block rounded-lg px-3 py-2 text-base font-medium {activePath === '/' ? 'bg-indigo-50 text-indigo-600 font-semibold' : 'text-slate-700 hover:bg-slate-50'}"
					onclick={closeMobileMenu}
				>
					Home
				</a>
				<a
					href="/tasks"
					class="block rounded-lg px-3 py-2 text-base font-medium {activePath.startsWith('/tasks') ? 'bg-indigo-50 text-indigo-600 font-semibold' : 'text-slate-700 hover:bg-slate-50'}"
					onclick={closeMobileMenu}
				>
					Browse Tasks
				</a>
				{#if loggedIn}
					<a
						href={dashboardUrl}
						class="block rounded-lg px-3 py-2 text-base font-medium {activePath.startsWith('/dashboard') ? 'bg-indigo-50 text-indigo-600 font-semibold' : 'text-slate-700 hover:bg-slate-50'}"
						onclick={closeMobileMenu}
					>
						My Dashboard
					</a>
				{/if}
			</nav>

			<div class="pt-3 border-t border-slate-100">
				{#if loggedIn && user}
					<div class="flex items-center justify-between py-2">
						<div>
							<div class="font-bold text-slate-900">{user.name || user.email}</div>
							<div class="text-xs text-indigo-600 font-medium">{formatRole(user.role)}</div>
						</div>
						<button
							type="button"
							class="rounded-lg border border-slate-200 px-3 py-1.5 text-xs font-semibold text-rose-600 hover:bg-rose-50"
							onclick={handleLogout}
						>
							Logout
						</button>
					</div>
				{:else}
					<div class="flex flex-col gap-2 pt-2">
						<a
							href="/login"
							class="w-full text-center rounded-lg border border-slate-200 py-2.5 text-sm font-semibold text-slate-700 hover:bg-slate-50"
							onclick={closeMobileMenu}
						>
							Log In
						</a>
						<a
							href="/register"
							class="w-full text-center rounded-lg bg-indigo-600 py-2.5 text-sm font-semibold text-white shadow-xs hover:bg-indigo-700"
							onclick={closeMobileMenu}
						>
							Get Started Free
						</a>
					</div>
				{/if}
			</div>
		</div>
	{/if}
</header>
