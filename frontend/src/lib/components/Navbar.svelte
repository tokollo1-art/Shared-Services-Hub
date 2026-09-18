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

<header class="sticky top-0 z-40 w-full border-b border-[#C8D4E8] bg-[#FFFFFF]/95 backdrop-blur-xs">
	<div class="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
		<!-- Brand / Logo -->
		<div class="flex items-center gap-3">
			<a href="/" class="flex items-center gap-3 group" onclick={closeMobileMenu}>
				<!-- Sharp Architectural Mark -->
				<div class="flex h-9 w-9 items-center justify-center bg-[#1446A0] text-white font-mono font-bold text-sm tracking-wider shadow-xs border border-[#1446A0]">
					SSH
				</div>
				<div>
					<div class="flex items-center gap-2">
						<span class="font-serif text-lg font-bold tracking-tight text-[#0D1B3E]">Shared Services Hub</span>
						<span class="bg-[#EFF4FF] border border-[#C8D4E8] px-1.5 py-0.2 text-[10px] font-mono font-bold text-[#1446A0]">ZA</span>
					</div>
					<p class="text-[11px] font-sans text-[#6A84AC] hidden sm:block tracking-normal">
						South African Social Impact Workforce Platform
					</p>
				</div>
			</a>
		</div>

		<!-- Desktop Navigation Links -->
		<nav class="hidden md:flex items-center gap-1">
			<a
				href="/"
				class="px-3.5 py-2 text-sm font-medium transition-colors {activePath === '/' ? 'text-[#1446A0] font-semibold border-b-2 border-[#1446A0]' : 'text-[#0D1B3E] hover:text-[#1446A0] hover:bg-[#F8F9FC]'}"
			>
				Home
			</a>

			<a
				href="/tasks"
				class="px-3.5 py-2 text-sm font-medium transition-colors {activePath.startsWith('/tasks') ? 'text-[#1446A0] font-semibold border-b-2 border-[#1446A0]' : 'text-[#0D1B3E] hover:text-[#1446A0] hover:bg-[#F8F9FC]'}"
			>
				Task Directory
			</a>

			{#if loggedIn}
				<a
					href={dashboardUrl}
					class="px-3.5 py-2 text-sm font-medium transition-colors {activePath.startsWith('/dashboard') ? 'text-[#1446A0] font-semibold border-b-2 border-[#1446A0]' : 'text-[#0D1B3E] hover:text-[#1446A0] hover:bg-[#F8F9FC]'}"
				>
					Dashboard
				</a>
			{/if}
		</nav>

		<!-- Right Side: User Profile / Auth Action Buttons -->
		<div class="hidden md:flex items-center gap-3">
			{#if loggedIn && user}
				<div class="flex items-center gap-3 pl-3 border-l border-[#C8D4E8]">
					<div class="text-right">
						<div class="text-sm font-bold text-[#0D1B3E] leading-tight">{user.name || user.email}</div>
						<div class="mt-0.5">
							<span class="inline-block bg-[#EFF4FF] border border-[#C8D4E8] px-2 py-0.5 text-[10px] font-mono font-semibold text-[#1446A0] uppercase">
								{formatRole(user.role)}
							</span>
						</div>
					</div>

					<a
						href={dashboardUrl}
						class="flex h-8 w-8 items-center justify-center bg-[#F8F9FC] border border-[#C8D4E8] text-[#1446A0] font-mono font-bold text-xs hover:border-[#1446A0] transition-colors"
						title="Go to Dashboard"
					>
						{(user.name || 'U').charAt(0).toUpperCase()}
					</a>

					<button
						type="button"
						class="border border-[#C8D4E8] bg-white px-3 py-1.5 text-xs font-semibold text-[#0D1B3E] hover:bg-[#F8F9FC] hover:border-[#C42B2B] hover:text-[#C42B2B] transition-colors"
						onclick={handleLogout}
					>
						Sign Out
					</button>
				</div>
			{:else}
				<div class="flex items-center gap-2.5">
					<a
						href="/login"
						class="px-4 py-2 text-sm font-medium text-[#0D1B3E] hover:text-[#1446A0] transition-colors"
					>
						Sign In
					</a>
					<a
						href="/register"
						class="cape-btn-primary px-4 py-2 text-sm font-semibold shadow-xs"
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
				class="p-2 text-[#0D1B3E] hover:bg-[#F8F9FC] border border-[#C8D4E8] focus:outline-none"
				onclick={toggleMobileMenu}
				aria-expanded={mobileMenuOpen}
				aria-label="Toggle navigation menu"
			>
				{#if mobileMenuOpen}
					<svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
						<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
					</svg>
				{:else}
					<svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
						<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
					</svg>
				{/if}
			</button>
		</div>
	</div>

	<!-- Mobile Dropdown Menu -->
	{#if mobileMenuOpen}
		<div class="md:hidden border-b border-[#C8D4E8] bg-white px-4 pt-2 pb-6 space-y-3">
			<nav class="space-y-1">
				<a
					href="/"
					class="block px-3 py-2 text-sm font-medium {activePath === '/' ? 'bg-[#EFF4FF] text-[#1446A0] font-bold' : 'text-[#0D1B3E] hover:bg-[#F8F9FC]'}"
					onclick={closeMobileMenu}
				>
					Home
				</a>
				<a
					href="/tasks"
					class="block px-3 py-2 text-sm font-medium {activePath.startsWith('/tasks') ? 'bg-[#EFF4FF] text-[#1446A0] font-bold' : 'text-[#0D1B3E] hover:bg-[#F8F9FC]'}"
					onclick={closeMobileMenu}
				>
					Task Directory
				</a>
				{#if loggedIn}
					<a
						href={dashboardUrl}
						class="block px-3 py-2 text-sm font-medium {activePath.startsWith('/dashboard') ? 'bg-[#EFF4FF] text-[#1446A0] font-bold' : 'text-[#0D1B3E] hover:bg-[#F8F9FC]'}"
						onclick={closeMobileMenu}
					>
						Dashboard
					</a>
				{/if}
			</nav>

			<div class="pt-3 border-t border-[#C8D4E8]">
				{#if loggedIn && user}
					<div class="flex items-center justify-between py-2">
						<div>
							<div class="font-bold text-[#0D1B3E]">{user.name || user.email}</div>
							<div class="text-xs text-[#1446A0] font-mono">{formatRole(user.role)}</div>
						</div>
						<button
							type="button"
							class="border border-[#C8D4E8] px-3 py-1.5 text-xs font-semibold text-[#C42B2B] hover:bg-rose-50"
							onclick={handleLogout}
						>
							Sign Out
						</button>
					</div>
				{:else}
					<div class="flex flex-col gap-2 pt-2">
						<a
							href="/login"
							class="w-full text-center border border-[#C8D4E8] py-2.5 text-sm font-semibold text-[#0D1B3E] hover:bg-[#F8F9FC]"
							onclick={closeMobileMenu}
						>
							Sign In
						</a>
						<a
							href="/register"
							class="w-full text-center cape-btn-primary py-2.5 text-sm font-semibold shadow-xs"
							onclick={closeMobileMenu}
						>
							Get Started
						</a>
					</div>
				{/if}
			</div>
		</div>
	{/if}
</header>
